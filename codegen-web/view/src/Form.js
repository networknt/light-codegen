import React, {Component} from 'react';
import { withStyles } from '@material-ui/core/styles';
import CircularProgress from '@material-ui/core/CircularProgress';
import Button from '@material-ui/core/Button';
import SchemaForm from 'react-schema-form/lib/SchemaForm';
import utils from 'react-schema-form/lib/utils';
import forms from './forms';

const formId = 'codeGenForm';

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
        let formData = forms[formId];
        this.state = {
            formId: formId,
            schema: formData.schema,
            form: formData.form,
            actions: formData.actions,
            model: formData.model || {}
        }
    }

    onModelChange = (key, val) => {
        const { model } = this.state;
        const newModel = model;
        utils.selectOrSet(key, this.state.model, val);
        this.setState({ model: newModel });
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
        if(this.state.schema) {
            var actions = [];
            this.state.actions.map((item, index) => {
                let boundButtonClick = this.onButtonClick.bind(this, item);
                actions.push(<Button variant="contained" className={classes.button} color="primary" key={index} onClick={boundButtonClick}>{item.title}</Button>)
            });
            let wait;
            if(this.state.fetching) {
                console.log('fetching is true');
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

export default withStyles(styles)(Form);
