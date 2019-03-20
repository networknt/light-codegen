import React, {Component} from 'react';
import { withStyles } from '@material-ui/core/styles';
import { submitForm} from "../actions";
import connect from "react-redux/es/connect/connect";
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import SchemaForm from 'react-schema-form/lib/SchemaForm';
import utils from 'react-schema-form/lib/utils';
import forms from '../data/Forms';

const styles = theme => ({
    root: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    formControl: {
        margin: theme.spacing.unit,
        minWidth: 120,
    },
    selectEmpty: {
        marginTop: theme.spacing.unit * 2,
    },
    progress: {
        margin: theme.spacing.unit * 2,
    },
    button: {
        margin: theme.spacing.unit,
    },
});

class Form extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fetching: false,
            error: null,
            formId: null,
            schema: null,
            form: null,
            actions: null,
            model: {}
        }
    }

    componentWillUpdate(nextProps, nextState, nextContext) {
        if(this.state.formId !== nextProps.match.params.formId) {
            let formData = forms[this.props.match.params.formId];
            this.setState({
                formId: nextProps.match.params.formId,
                schema: formData.schema,
                form: formData.form,
                actions: formData.actions,
                model: formData.model || {}
            });
        }
    }

    componentDidMount() {
        let formData = forms[this.props.match.params.formId];
        this.setState({
            formId: this.props.match.params.formId,
            schema: formData.schema,
            form: formData.form,
            actions: formData.actions,
            model: formData.model || {}
        });
    }

    onModelChange = (key, val) => {
        //console.log(this.state.model);
        utils.selectOrSet(key, this.state.model, val);
    };

    onButtonClick(action) {
        console.log(action, this.state.model);
        let validationResult = utils.validateBySchema(this.state.schema, this.state.model);
        if(!validationResult.valid) {
            this.setState({error: validationResult.error.message});
        } else {
            action.data = this.state.model;
            this.setState({success: action.success, fetching: true});
            this.props.submitForm(action);
        }
    }

    render() {
        const { classes } = this.props;
        //console.log(this.state.actions);
        if(this.state.schema) {
            var actions = [];
            this.state.actions.map((item, index) => {
                let boundButtonClick = this.onButtonClick.bind(this, item);
                actions.push(<Button variant="contained" className={classes.button} color="primary" key={index} onClick={boundButtonClick}>{item.title}</Button>)
            });
            let wait;
            if(this.state.fetching) {
                //console.log('fetching is true');
                wait = <div><CircularProgress className={classes.progress} /></div>;
            } else {
                //console.log("fetching is false");
                wait = <div></div>;
            }
            let title = <h2>{this.state.schema.title}</h2>

            return (
                <div>
                    {wait}
                    {title}
                    <SchemaForm schema={this.state.schema} form={this.state.form} model={this.state.model} onModelChange={this.onModelChange} />
                    <pre>{this.state.error}</pre>
                    {actions}
                </div>
            )
        } else {
            return (<CircularProgress className={classes.progress} />);
        }
    }
}

const mapStateToProps = state => ({
});

const mapDispatchToProps = dispatch => ({
    submitForm: action => dispatch(submitForm(action))
});

export default connect(
    mapStateToProps,
    mapDispatchToProps
)(withStyles(styles)(Form));
